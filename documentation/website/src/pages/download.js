// THIS FILE IS GENERATED
import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';
        const features = [
        {
        title: <>Download Installer</>,
imageUrl: 'img/run.png',
description: (
<>
<p>
Download a graphical installer that will make it simple to install nuts package manager.
After downloading the installer, just double click the file and follow the installation wizard.
A valid java 1.8+ runtime is required.
</p>
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-installer.jar'}
                    target="_blank"
                    >

Portable Installer
</Link>
</>
),
},
{
                title: <>Download Raw Jar Package</>,
imageUrl: 'img/jar2.png',
description: (
<>
<p>
Download raw jar file to perform installation using your favourite shell.
After downloading the installer, follow the documentation to install the package manager.
Use 'Portable' version for production and 'Preview' for all other cases.
A valid java 1.8+ runtime is required.
</p>
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    href={'https://thevpc.net/nuts-stable.jar'}
                    target="_blank"
                    >

Stable 0.8.3 Jar
</Link>
    <Link
                        className={clsx(
                            'button button--secondary b3',
                            styles.getStarted,
                            )}
                        href={'https://thevpc.net/nuts-preview.jar'}
                        target="_blank"
                        >

                    Preview 0.8.3 Jar
                    </Link>

</>
),
},
,
{
                title: <>Install Manually</>,
imageUrl: 'img/terminal.png',
description: (
<>
<p>
Use a one commandline to download and install Nuts package manager using cUrl command.
Use 'Portable' version for production and 'Preview' for all other cases.
A valid java 1.8+ runtime is required.
</p>
<Link
                    className={clsx(
                        'button button--secondary--lg b2 ',
                        styles.getStarted,
                        )}
                    >Stable</Link>
<pre>
curl -sOL https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar -o nuts.jar && java -jar nuts.jar -Zy
</pre>

<Link
                    className={clsx(
                            'button button--secondary b3',
                        styles.getStarted,
                        )}
                    >Preview</Link>
<pre>
curl -sOL https://thevpc.net/nuts-preview.jar -o nuts.jar && java -jar nuts.jar -Zy
</pre>

</>
),
},

];

function Feature({imageUrl, title, description}) {
                const imgUrl = useBaseUrl(imageUrl);
                return (
                        <div className={clsx('col col--4', styles.feature)}>
    {imgUrl && (
                        <div className="text--center">
        <img className={styles.featureImage} src={imgUrl} alt={title} />
    </div>
                        )}
    <h3>{title}</h3>
    <p>{description}</p>
</div>
                        );
}

function Download() {
                const context = useDocusaurusContext();
                const {siteConfig = {}} = context;
                return (
                        <Layout
    title={`${siteConfig.title}` + ', the Java Package Manager'}
    description="Description will go into a meta tag in <head />">
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
            <h1 className="hero__title">{siteConfig.title}</h1>
            <p className="hero__subtitle">{siteConfig.tagline}</p>
            <img src="https://thevpc.net/nuts/images/pixel.gif?q=nuts-gsite" alt="" />
            Select your download format...
        </div>
    </header>
    <main>
        {features && features.length > 0 && (
                        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {features.map((props, idx) => (
                                <Feature key={idx} {...props} />
                                ))}
                </div>
            </div>
        </section>
                        )}
    </main>
</Layout>
                        );
}

export default Download;
